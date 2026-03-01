"use client";

import NoticeContextInterface from "@/types/NoticeContextInterface";
import { createContext } from "react";

const NoticeContext = createContext<NoticeContextInterface>({
    noticeList: [],
    setNoticeList: null,
});

export default NoticeContext;
