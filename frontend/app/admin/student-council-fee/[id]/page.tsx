import API_SERVER from "@/apiServer";
import StudentCouncilFeeSinglePage from "@/components/admin/student-council/single/StudentCouncilFeeSinglePage";
import PathParamsInterface from "@/types/PathParamsInterface";
import { cookies } from "next/headers";

export default async function Page({ params }: PathParamsInterface) {
    const { id } = await params;
    // 여기서 presignedURL을 서버로부터 받아온다.

    const cookieStore = await cookies();
    const cookie = cookieStore.toString();

    const res = await fetch(`${API_SERVER}/student-council-fee-verification/${id}/image`, {
        method: "GET",
        headers: {
            cookie,
        },
    });

    const json = await res.json();
    const presignedURL = json.data;
    return <StudentCouncilFeeSinglePage id={Number(id)} presignedURL={presignedURL} />;
}
