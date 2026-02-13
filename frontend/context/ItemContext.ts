"use client";

import ItemContextInterface from "@/types/ItemContextInterface";
import { createContext } from "react";

const ItemContext = createContext<ItemContextInterface>({
    itemList: [],
    setItemList: null,
});

export default ItemContext;
