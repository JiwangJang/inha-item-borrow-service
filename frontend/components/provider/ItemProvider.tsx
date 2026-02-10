"use client";

import ItemContext from "@/context/ItemContext";
import mockItems from "@/mockData/mockItems";
import ItemInterface from "@/types/ItemInterface";
import React, { useState } from "react";

export default function ItemProvider({
    initialValue,
    children,
}: {
    initialValue: ItemInterface[];
    children: React.ReactNode;
}) {
    // 개발용
    const [itemList, setItemList] = useState(mockItems);
    return <ItemContext.Provider value={{ itemList, setItemList }}>{children}</ItemContext.Provider>;
}
