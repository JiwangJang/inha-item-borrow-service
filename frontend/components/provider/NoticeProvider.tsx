"use client";

import NoticeContext from "@/context/NoticeContext";
import NoticeInterface from "@/types/NoticeInterface";
import React, { useState } from "react";

export default function NoticeProvider({
    initialValue,
    children,
}: {
    initialValue: NoticeInterface[];
    children: React.ReactNode;
}) {
    const [noticeList, setNoticeList] = useState(initialValue);
    return (
        <NoticeContext.Provider
            value={{
                noticeList,
                setNoticeList,
            }}
        >
            {children}
        </NoticeContext.Provider>
    );
}
