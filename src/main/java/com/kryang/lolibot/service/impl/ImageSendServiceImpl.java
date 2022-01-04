package com.kryang.lolibot.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kryang.lolibot.pojo.ImageEntity;
import com.kryang.lolibot.service.ImageSendService;
import com.kryang.lolibot.service.ImageService;
import com.kryang.lolibot.util.HttpClientUtil;
import com.kryang.lolibot.util.Randoms;
import lombok.extern.slf4j.Slf4j;
import net.lz1998.pbbot.utils.Msg;
import onebot.OnebotEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.net.www.http.HttpClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class ImageSendServiceImpl implements ImageSendService {

    @Autowired
    private ImageService imageService;

    private static final String WIFE_URL = "https://www.thiswaifudoesnotexist.net/example-%s.jpg";
    private static final String LOLI_URL = "https://api.lolicon.app/setu/v2";

    @Override
    public Msg getSetu(OnebotEvent.GroupMessageEvent event) {
//        if (event.getMessageType().equals("text")) {
//            String message = event.getRawMessage();
//            if (message.contains("涩图") || message.contains("色图")) {
//                ImageEntity img = imageService.getById(Randoms.randomInt(imageService.getMaxId()));
//                return Msg.builder().image(img.getUrl());
//            }
//        }
        return null;
    }

    @Override
    public Msg getWife(OnebotEvent.GroupMessageEvent event) {
        String message = event.getRawMessage();
        if (message.contains("wife") || message.contains("随机老婆")) {
            String seed = Randoms.randomInt(5);
            String url = String.format(WIFE_URL, seed);
            log.info("发送图片:{}", url);
            return Msg.builder().at(event.getUserId()).text(String.format("你的第%s位老婆:", seed)).image(url);
        }
        return null;
    }

    @Override
    public Msg getLoli(OnebotEvent.GroupMessageEvent event) {
        String message = event.getRawMessage();
        if (message.contains("loli") || message.contains("来份萝莉")) {
            Map<String, Object> headerMap = new HashMap<>();
            String s = HttpClientUtil.doGet(LOLI_URL, headerMap, "");
            JSONObject jsonObject = JSON.parseObject(s);
            String url = jsonObject.getJSONArray("data").getJSONObject(0).getJSONObject("urls").getString("original");
            if (message.contains("详细信息")){
                StringBuffer sb = new StringBuffer();
                sb.append("pid:").append(jsonObject.getJSONArray("data").getJSONObject(0).getString("uid")).append("\n");
                sb.append("url:").append(url).append("\n");
                sb.append("tags:").append(jsonObject.getJSONArray("data").getJSONObject(0).getJSONArray("tags").toString()).append("\n");
                return Msg.builder().text(sb.toString()).image(url);
            }
            return Msg.builder().at(event.getUserId()).image(url);
        }
        return null;
    }

    @Override
    public Msg saveSetu(OnebotEvent.GroupMessageEvent event) {
//        if (messageChain.size() > 1) {
//            if (messageChain.get(0).getType().equals("at")) {
//                String text = messageChain.get(1).getDataMap().get("image");
//                if (text.contains("涩图") || text.contains("色图")) {
//                    ImageEntity img = imageService.getById(Randoms.randomInt(imageService.getMaxId()));
//                    return Msg.builder().image(img.getUrl());
//                }
//            }
//        }
        return null;
    }
}
