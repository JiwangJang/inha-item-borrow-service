"use client";

import ItemContext from "@/context/ItemContext";
import ItemInterface from "@/types/ItemInterface";
import React, { useState } from "react";

export default function BorrowerItemProvider({
    initialValue,
    children,
}: {
    initialValue: ItemInterface[];
    children: React.ReactNode;
}) {
    // 개발용
    const [itemList, setItemList] = useState(initialValue);
    return <ItemContext.Provider value={{ itemList, setItemList }}>{children}</ItemContext.Provider>;
}
