import AdminNoticeSinglePage from "@/components/admin/notice/single/AdminNoticeSinglePage";
import PathParamsInterface from "@/types/PathParamsInterface";

export default async function Page({ params }: PathParamsInterface) {
    const { id } = await params;

    return <AdminNoticeSinglePage noticeId={id} />;
}
