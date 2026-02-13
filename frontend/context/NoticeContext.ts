"use client";

import NoticeInterface from "@/types/NoticeInterface";
import { createContext } from "react";

const NoticeContext = createContext<NoticeInterface[]>([]);

export default NoticeContext;
