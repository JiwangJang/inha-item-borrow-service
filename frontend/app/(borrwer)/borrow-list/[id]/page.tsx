import SingleBorrowRequestPage from "@/components/borrower/borrow-list/single/SingleBorrowRequestPage";
import PathParamsInterface from "@/types/PathParamsInterface";

export default async function Page({ params }: PathParamsInterface) {
    const { id } = await params;

    return <SingleBorrowRequestPage requestId={id} />;
}
