import AdminRequestContextInterface from "@/types/AdminRequestContextInterface";
import { createContext } from "react";

const AdminRequestContext = createContext<AdminRequestContextInterface>({
    requestList: [],
    setRequestList: null,
});

export default AdminRequestContext;
