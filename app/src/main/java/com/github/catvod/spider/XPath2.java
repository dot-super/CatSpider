package com.github.catvod.spider;

import com.github.catvod.crawler.SpiderDebug;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XPath2 extends XPath {
    //添加头文件
    private HashMap<String,String> header = new HashMap<>();
    private HashMap<String,String> playHeader = new HashMap<>();

    @Override
    protected void loadRuleExt(String json) {
        super.loadRuleExt(json);
        try {
            JSONObject jsonObj = new JSONObject(json);
            JSONObject navs = jsonObj.optJSONObject("header");
            if (navs != null) {
                Iterator<String> keys = navs.keys();
                while (keys.hasNext()) {
                    String name = keys.next();
                    header.put(name.trim(), navs.getString(name).trim());
                }
            }
            navs = jsonObj.optJSONObject("playHeader");
            if (navs != null) {
                Iterator<String> keys = navs.keys();
                while (keys.hasNext()) {
                    String name = keys.next();
                    playHeader.put(name.trim(), navs.getString(name).trim());
                }
            }
        } catch (JSONException e) {
            SpiderDebug.log(e);
        }
    }

    @Override
    protected HashMap<String, String> getHeaders(String url) {
        if (header.isEmpty()) {
            header.put("User-Agent", rule.getUa().isEmpty()
                    ? "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.54 Safari/537.36"
                    : rule.getUa());
        }
        return header;
    }

    @Override
    protected String categoryUrl(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        String cateUrl = rule.getCateUrl();
        if (filter && extend != null && extend.size() > 0) {
            for (Iterator<String> it = extend.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                String value = extend.get(key);
                if (value.length() > 0) {
                    cateUrl = cateUrl.replace("{" + key + "}", URLEncoder.encode(value));
                }
            }
        }
        if (cateUrl.contains("{catePg-0}")) {
            pg = Integer.toString(Integer.parseInt(pg) - 1);
            cateUrl = cateUrl.replace("{cateId}", tid).replace("{catePg-0}", pg);
        } else {
            cateUrl = cateUrl.replace("{cateId}", tid).replace("{catePg}", pg);
        }
        Matcher m = Pattern.compile("\\{(.*?)\\}").matcher(cateUrl);
        while (m.find()) {
            String n = m.group(0).replace("{", "").replace("}", "");
            cateUrl = cateUrl.replace(m.group(0), "").replace("/" + n + "/", "");
        }
        return cateUrl;
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        try {
            fetchRule();
            String webUrl = rule.getPlayUrl().isEmpty() ? id : rule.getPlayUrl().replace("{playUrl}", id);
            SpiderDebug.log(webUrl);
            JSONObject result = new JSONObject();
            result.put("parse", 1);
            result.put("playUrl", "");
            if (!header.isEmpty()) {
                result.put("header", playHeader.isEmpty() ? header.toString() : playHeader.toString());
            } else if (!rule.getUa().isEmpty()) {
                result.put("ua", rule.getPlayUa().isEmpty() ? rule.getUa() : rule.getPlayUa());
            }
            result.put("url", webUrl);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

}