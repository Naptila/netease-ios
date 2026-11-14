package me.naptila.neteaseios.bridge

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Locale

/** Bounded, single-request local HTTP parser; request bodies are never logged. */
internal class HttpRequest(
    val method: String,
    val path: String,
    val headers: Map<String, String>,
    val body: ByteArray
) {
    companion object {
        private val CONTENT_LENGTH_REGEX = Regex("^[0-9]{1,5}$")

        fun read(input: InputStream): HttpRequest {
            val head = ByteArrayOutputStream()
            var end = 0
            while (end != 4) {
                val b = input.read()
                if (b < 0) throw IOException("incomplete-header")
                head.write(b)
                if (head.size() > 8192) throw IOException("header-too-large")
                end = when {
                    (end == 0 || end == 2) && b == '\r'.code -> end + 1
                    (end == 1 || end == 3) && b == '\n'.code -> end + 1
                    b == '\r'.code -> 1
                    else -> 0
                }
            }

            val rawHeader = String(head.toByteArray(), StandardCharsets.ISO_8859_1)
                .removeSuffix("\r\n\r\n")
            val lines = rawHeader.split("\r\n")
            val first = lines[0].split(' ')
            if (first.size != 3 || first[2] != "HTTP/1.1" || (first[0] != "GET" && first[0] != "POST")) {
                throw IOException("request-line")
            }
            val requestPath = first[1]
            if (!requestPath.startsWith("/") || requestPath.contains("..") || requestPath.contains("%")) {
                throw IOException("request-path")
            }

            val headers = HashMap<String, String>()
            for (i in 1 until lines.size) {
                val line = lines[i]
                val colon = line.indexOf(':')
                if (colon < 1) throw IOException("header-format")
                val name = line.substring(0, colon).trim().lowercase(Locale.ROOT)
                val value = line.substring(colon + 1).trim()
                if (headers.containsKey(name)) throw IOException("duplicate-header")
                headers[name] = value
            }

            if (headers.containsKey("transfer-encoding")) {
                throw IOException("unsupported-transfer")
            }

            val lengthHeader = headers["content-length"]
            var count = 0
            if (lengthHeader != null) {
                if (!CONTENT_LENGTH_REGEX.matches(lengthHeader)) {
                    throw IOException("body-length")
                }
                count = lengthHeader.toInt()
                if (count > 4096) throw IOException("body-too-large")
            }
            if (first[0] == "POST" && lengthHeader == null) {
                throw IOException("body-length-missing")
            }

            val body = ByteArray(count)
            var offset = 0
            while (offset < count) {
                val got = input.read(body, offset, count - offset)
                if (got < 0) throw IOException("incomplete-body")
                offset += got
            }
            return HttpRequest(
                method = first[0],
                path = requestPath,
                headers = headers,
                body = body
            )
        }
    }
}
