import DivisionContextInterface from "@/types/DivisionContextInterface";
import { createContext } from "react";

const DivisionContext = createContext<DivisionContextInterface>({
    divisionList: [],
    setDivisionList: null,
});

export default DivisionContext;
