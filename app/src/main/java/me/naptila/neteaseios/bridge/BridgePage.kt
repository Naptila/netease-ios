package me.naptila.neteaseios.bridge

import java.util.Locale

/** Pure page renderer executed inside the target game's GMWebviewActivity. */
internal object BridgePage {
    fun render(
        appLabel: String,
        base: String
    ): String {
        val guidance = "请返回游戏点「切换账号」重登（若弹窗原本已打开，先点右上角「×」重开）。"
        val badgeText = escapeHtml("网易 iOS 登录桥接 · $appLabel")

        return buildString {
            append("<!doctype html><html><head><meta charset=\"utf-8\">")
            append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
            append("<style>")
            append("body{margin:0;min-height:100vh;display:flex;align-items:center;justify-content:center;background:rgba(32,12,24,.62);color:#f4e2ea;font:15px/1.6 -apple-system,BlinkMacSystemFont,sans-serif;padding:22px;box-sizing:border-box}")
            append(".spin{width:26px;height:26px;border:3px solid rgba(248,163,191,.22);border-top-color:#f8a3bf;border-radius:50%;margin:0 auto 14px;animation:sp 1s linear infinite}.card{max-width:330px;width:100%;background:linear-gradient(165deg,#3a2030,#24121c);border-radius:20px;padding:24px 22px;box-shadow:0 18px 44px rgba(0,0,0,.5);border:1px solid rgba(242,120,159,.28)}")
            append(".badge{display:inline-block;padding:4px 12px;border-radius:999px;background:rgba(242,120,159,.22);color:#f8a3bf;font-size:11.5px;font-weight:700;margin-bottom:12px}")
            append("h2{margin:0 0 10px;font-size:20px;color:#f8fafc}")
            append("#status{margin:0 0 14px;color:#cfa9ba;font-size:14px}")
            append(".warn{margin:0 0 18px;padding:11px 13px;border-radius:12px;background:rgba(255,217,230,.16);border:1px solid rgba(242,120,159,.45);color:#f8b8cd;font-size:13px;line-height:1.55;font-weight:600}")
            append("button{appearance:none;border:0;border-radius:12px;background:#e85586;color:#fff;font-size:14.5px;font-weight:700;padding:12px 20px;width:100%;cursor:pointer}")
            append("@keyframes sp{to{transform:rotate(360deg)}}</style></head><body>")
            append("<div class=\"card\">")
            append("<span class=\"badge\">").append(badgeText).append("</span>")
            append("<h2>准备 iOS 登录</h2>")
            append("<div class=\"spin\"></div>")
            append("<p id=\"status\">正在读取当前账号…</p>")
            append("<div class=\"warn\">回游戏点「切换账号」重登即可；若弹窗此前已打开，请先点右上角「×」重开，切勿直接点旧列表！</div>")
            append("<button onclick=\"window.ntesBridgeClose()\">返回游戏</button>")
            append("</div>")
            append("<script>(function(){console.log=function(){};window.ntesBridgeClose=userClose;")
            append("var complete=false,sending=false,tries=0,importing=false;")
            append("var base=").append(quote(base)).append(";")
            append("function show(s){document.getElementById('status').textContent=s;}")
            append("function closePage(){AndroidJSBridge.postMsgToNative(JSON.stringify({methodId:'navigation_bar_func',reqData:{methodId:'close',action:'close'}}));}")
            append("function userClose(){if(importing&&!complete){finishImport(false);return;}closePage();}")
            append("function post(route,j){return fetch(base+route,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(j)}).then(function(r){if(!r.ok)throw Error();return r.json();});}")
            append("function fail(reason,text){if(complete)return;complete=true;show(text);post('/failed',{reason:reason}).catch(function(){});}")
            append("function finishImport(confirmed){if(complete)return;complete=true;show(confirmed?'SDK 已接受登录准备。':'请求已发送，但未收到 SDK 确认，请在游戏中核对结果。');post('/submitted',{confirmed:confirmed}).then(function(){show(").append(quote(guidance)).append(");setTimeout(closePage,250);}).catch(function(){show('请返回游戏核对登录结果。');});}")
            append("function receiveImport(raw){if(complete||!importing)return;try{var j=typeof raw==='string'?JSON.parse(raw):raw;if(!j)return;if(j.errCode===0||j.errCode==='0'){finishImport(true);return;}if(j.errCode!==undefined&&Number(j.errCode)!==0){var m=String(j.errMsg||'');fail(m==='File content is empty'?'sdk_read_failed':m==='No valid login info found'?'sdk_invalid_session':'sdk_rejected','SDK 未接受本次准备，请返回助手查看结果。');}}catch(e){}}")
            append("function receive(raw){if(complete||sending||importing)return;try{var j=typeof raw==='string'?JSON.parse(raw):raw;if(!j||j.executed_methodId!=='gameContextInfo')return;")
            append("sending=true;show('已读取当前账号，正在注入 iOS 登录准备…');post('/prepare',{full:String(j.user_name||''),game:String(j.game_id||''),channel:String(j.app_channel||'')}).then(function(v){")
            append("if(complete)return;if(!v.ok||!v.command){var r=String(v.reason||'prepare_failed');fail(r,r==='not_logged_in'?'未检测到已登录账号，请先在游戏内登录一次。':'准备文件写入未完成，请返回助手查看原因。');return;}")
            append("importing=true;var cmd=v.command;cmd.callback={nativeCallback:receiveImport};window.postMsgToNative(cmd);show('等待 SDK 接受登录准备（也可直接点下方返回游戏）…');setTimeout(function(){finishImport(false);},12000);")
            append("}).catch(function(){fail('request_failed','操作未完成，请返回助手重试。');}).finally(function(){sending=false;});")
            append("raw=null;j=null;}catch(e){fail('invalid_response','游戏客户端返回格式不兼容，请返回助手。');}}")
            append("function start(){if(complete||importing)return;if(typeof window.postMsgToNative!=='function'||!window.NTCallBackList){if(++tries<30)setTimeout(start,250);else fail('bridge_missing','连接未就绪，请先打开游戏登录一次。');return;}")
            append("window.postMsgToNative({methodId:'execute_extend_func',reqData:{methodId:'gameContextInfo',callback_mode:'web'},callback:{nativeCallback:receive}});")
            append("if(++tries<8)setTimeout(start,900);else setTimeout(function(){if(!complete&&!importing)fail('no_callback','未收到游戏接口响应，请返回助手重试。');},1800);}")
            append("window.addEventListener('load',function(){setTimeout(start,700);});})();</script></body></html>")
        }
    }

    private fun escapeHtml(value: String): String = buildString(value.length + 8) {
        for (c in value) {
            when (c) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&#39;")
                else -> append(c)
            }
        }
    }

    fun quote(value: String): String = buildString(value.length + 8) {
        append('"')
        for (c in value) {
            when {
                c == '"' || c == '\\' -> append('\\').append(c)
                c.code < 32 || c == '<' || c == '>' || c == '&' || c == '\u2028' || c == '\u2029' ->
                    append(String.format(Locale.ROOT, "\\u%04x", c.code))
                else -> append(c)
            }
        }
        append('"')
    }
}
