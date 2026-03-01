"use client";

import BorrowerContextInterface from "@/types/BorrowerContextInterface";
import { createContext } from "react";

const BorrowerContext = createContext<BorrowerContextInterface>({
    borrowerInfo: null,
    setBorrowerInfo: null,
});

export default BorrowerContext;
