import AdminNoticeRevisePage from "@/components/admin/notice/single/revise/AdminNoticeRevisePage";
import PathParamsInterface from "@/types/PathParamsInterface";

export default async function Page({ params }: PathParamsInterface) {
    const { id } = await params;

    return <AdminNoticeRevisePage noticeId={id} />;
}
