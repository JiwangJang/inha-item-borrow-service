"use client";

import ItemContext from "@/context/ItemContext";
import ItemInterface from "@/types/ItemInterface";
import React from "react";

export default function ItemProvider({
    initialValue,
    children,
}: {
    initialValue: ItemInterface[];
    children: React.ReactNode;
}) {
    return <ItemContext.Provider value={initialValue}>{children}</ItemContext.Provider>;
}
