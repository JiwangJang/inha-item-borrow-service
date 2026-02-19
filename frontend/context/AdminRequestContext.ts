import RequestContextInterface from "@/types/RequestContextInterface";
import { createContext } from "react";

const AdminRequestContext = createContext<RequestContextInterface>({
    requestList: [],
    setRequestList: null,
});

export default AdminRequestContext;
