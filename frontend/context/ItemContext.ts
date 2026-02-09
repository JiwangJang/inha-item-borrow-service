"use client";

import ItemInterface from "@/types/ItemInterface";
import { createContext } from "react";

const ItemContext = createContext<ItemInterface[]>([]);

export default ItemContext;
