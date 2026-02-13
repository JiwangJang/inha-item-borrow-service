import AdminStudentCouncilFeeContextInterface from "@/types/AdminStudentCouncilFeeContextInterface";
import { createContext } from "react";

const AdminStudentCouncilFeeContext = createContext<AdminStudentCouncilFeeContextInterface>({
    studentCouncilFeeList: [],
    setStudentCouncilFeeList: null,
});

export default AdminStudentCouncilFeeContext;
