import type { Metadata } from "next";
import "./globals.css";
import Header from "@/components/layout/header/Header";
import BottomNavigator from "@/components/layout/bottom-nav/BottomNavigator";
import NoticeProvider from "@/components/provider/NoticeProvider";
import getNotices from "@/utilities/getNotices";

export const metadata: Metadata = {
    title: "미래융합대학 물품대여시스템",
    description: "미래융합대학 물품대여시스템",
};

export default async function RootLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    const noticeProviderInitialValue = await getNotices();

    return (
        <html lang="ko">
            <body className="bg-slate-950 max-w-125 h-screen my-0 mx-auto relative flex flex-col overflow-hidden">
                <Header />
                <NoticeProvider initialValue={noticeProviderInitialValue}>
                    <div className="flex-1 overflow-y-auto">{children}</div>
                </NoticeProvider>
                <BottomNavigator />
            </body>
        </html>
    );
}
