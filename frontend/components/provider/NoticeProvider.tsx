"use client";

import NoticeContext from "@/context/NoticeContext";
import NoticeInterface from "@/types/NoticeInterface";
import React from "react";

export default function NoticeProvider({
    initialValue,
    children,
}: {
    initialValue: NoticeInterface[];
    children: React.ReactNode;
}) {
    return <NoticeContext.Provider value={initialValue}>{children}</NoticeContext.Provider>;
}
