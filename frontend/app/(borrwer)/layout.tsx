import BorrowerProvider from "@/components/provider/BorrowerProvider";
import checkLogin from "@/utilities/checkLogin";

export default async function Layout({ children }: { children: React.ReactNode }) {
    const borrowerInfo = await checkLogin();

    return (
        <div className="w-full pt-15 pb-16 bg-back min-h-screen common-px">
            <BorrowerProvider initialBorrowerInfo={borrowerInfo}>{children}</BorrowerProvider>
        </div>
    );
}
