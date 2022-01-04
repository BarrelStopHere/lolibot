package com.kryang.lolibot.plugin;

import com.kryang.lolibot.service.ImageSendService;
import lombok.extern.slf4j.Slf4j;
import net.lz1998.pbbot.bot.Bot;
import net.lz1998.pbbot.bot.BotPlugin;
import net.lz1998.pbbot.utils.Msg;
import onebot.OnebotEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoliPlugin extends BotPlugin {

    @Autowired
    private ImageSendService service;


    @Override
    public int onGroupMessage(@NotNull Bot bot, @NotNull OnebotEvent.GroupMessageEvent event) {
        Msg msg = service.getLoli(event);
        if (msg != null) bot.sendGroupMsg(event.getGroupId(), msg, false);
        return MESSAGE_IGNORE;
    }
}
