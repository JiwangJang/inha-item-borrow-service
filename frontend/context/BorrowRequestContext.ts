import RequestContextInterface from "@/types/RequestContextInterface";
import { createContext } from "react";

const BorrowRequestContext = createContext<RequestContextInterface>({
    requestList: [],
    setRequestList: null,
});

export default BorrowRequestContext;
