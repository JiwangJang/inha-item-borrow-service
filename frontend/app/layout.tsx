import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
    title: "미래융합대학 물품대여시스템",
    description: "미래융합대학 물품대여시스템",
};

export default function RootLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="ko">
            <body className="bg-slate-950 max-w-125 min-h-screen my-0 mx-auto">{children}</body>
        </html>
    );
}
