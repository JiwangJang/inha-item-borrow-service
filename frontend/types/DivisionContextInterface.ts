import React, { SetStateAction } from "react";
import DivisionInterface from "./DivisionInterface";

export default interface DivisionContextInterface {
    divisionList: DivisionInterface[];
    setDivisionList: React.Dispatch<SetStateAction<DivisionInterface[]>> | null;
}
