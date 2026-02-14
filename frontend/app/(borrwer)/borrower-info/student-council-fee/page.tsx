import { cookies } from "next/headers";
import API_SERVER from "@/apiServer";
import StudentCouncilFeePage from "@/components/borrower/borrower-info/student-council-fee/StudentCouncilFeePage";
import StudentCouncilFeeVerificationInterface from "@/types/StudentCouncilFeeVerificationInterface";

async function getMyStudentCouncilFeeVerification(): Promise<StudentCouncilFeeVerificationInterface | null> {
    try {
        const cookieStore = await cookies();
        const res = await fetch(`${API_SERVER}/student-council-fee-verification/single`, {
            method: "GET",
            headers: {
                Cookie: cookieStore.toString(),
            },
        });

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const data = await res.json();
        console.log(data);
        return data.data;
    } catch (error) {
        console.error(error);
        return null;
    }
}

export default async function Page() {
    const verification = await getMyStudentCouncilFeeVerification();
    // console.log(verification);
    return <StudentCouncilFeePage verification={verification} />;
}
