import BorrowRequestRevisePage from "@/components/borrower/borrow-list/single/revise/BorrowRequestRevisePage";
import PathParamsInterface from "@/types/PathParamsInterface";

export default async function Page({ params }: PathParamsInterface) {
    const { id } = await params;
    return <BorrowRequestRevisePage id={id} />;
}
