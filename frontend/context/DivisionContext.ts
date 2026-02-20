import DivisionInterface from "@/types/DivisionInterface";
import { createContext } from "react";

const DivisionContext = createContext<DivisionInterface[]>([]);

export default DivisionContext;
