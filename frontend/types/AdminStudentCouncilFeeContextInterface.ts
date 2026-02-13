import StudentCouncilFeeInterface from "./StudentCouncilFeeInterface";

export default interface AdminStudentCouncilFeeContextInterface {
    studentCouncilFeeList: StudentCouncilFeeInterface[];
    setStudentCouncilFeeList: React.Dispatch<React.SetStateAction<StudentCouncilFeeInterface[]>> | null;
}
