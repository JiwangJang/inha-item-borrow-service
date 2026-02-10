"use client";

import ItemContextInterface from "@/types/ItemContextInterface";
import { createContext } from "react";

const ItemContext = createContext<ItemContextInterface | null>(null);

export default ItemContext;
