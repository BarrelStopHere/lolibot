package com.kryang.lolibot.service.impl;

import com.kryang.lolibot.pojo.ImageEntity;
import com.kryang.lolibot.service.ImageSendService;
import com.kryang.lolibot.service.ImageService;
import com.kryang.lolibot.util.Randoms;
import lombok.extern.slf4j.Slf4j;
import net.lz1998.pbbot.utils.Msg;
import onebot.OnebotEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class ImageSendServiceImpl implements ImageSendService {

    @Autowired
    private ImageService imageService;

    private static final String WIFE_URL = "https://www.thiswaifudoesnotexist.net/example-%s.jpg";

    @Override
    public Msg getSetu(OnebotEvent.GroupMessageEvent event) {
        if (event.getMessageType().equals("text")) {
            String message = event.getRawMessage();
            if (message.contains("涩图") || message.contains("色图")) {
                ImageEntity img = imageService.getById(Randoms.randomInt(imageService.getMaxId()));
                return Msg.builder().image(img.getUrl());
            }
        }
        return null;
    }

    @Override
    public Msg getWife(OnebotEvent.GroupMessageEvent event) {
        String message = event.getRawMessage();
        if (message.contains("wife") || message.contains("随机老婆")) {
            String seed = Randoms.randomInt(5);
            String url = String.format(WIFE_URL, seed);
            log.info("发送图片:{}", url);
            return Msg.builder().at(event.getUserId()).text(String.format("no.%s wife:",url)).image(url);
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
