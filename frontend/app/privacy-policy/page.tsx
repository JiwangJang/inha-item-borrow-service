import V1AgreementSection from "@/components/borrower/borrower-info/agreement/v1/V1AgreementSection";

export default function Page() {
    return (
        <div className="w-full pt-15 pb-16 min-h-[calc(100dvh-60px)] bg-back common-px">
            <div className="h-5"></div>
            <V1AgreementSection />
        </div>
    );
}
