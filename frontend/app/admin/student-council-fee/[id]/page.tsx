import StudentCouncilFeeSinglePage from "@/components/admin/student-council/single/StudentCouncilFeeSinglePage";
import PathParamsInterface from "@/types/PathParamsInterface";

export default async function Page({ params }: PathParamsInterface) {
    const { id } = await params;

    return <StudentCouncilFeeSinglePage id={Number(id)} />;
}
