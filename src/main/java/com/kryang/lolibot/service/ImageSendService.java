package com.kryang.lolibot.service;

import net.lz1998.pbbot.utils.Msg;
import onebot.OnebotEvent;


public interface ImageSendService {

    Msg getSetu(OnebotEvent.GroupMessageEvent event);

    Msg getWife(OnebotEvent.GroupMessageEvent event);

    Msg saveSetu(OnebotEvent.GroupMessageEvent event);

}
