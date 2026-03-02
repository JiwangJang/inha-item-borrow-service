// app/api/[...path]/route.ts
import { NextRequest, NextResponse } from "next/server";

export const runtime = "nodejs"; // 쿠키/헤더 다루기엔 이게 덜 골치 아픔

const BACKEND = process.env.BACKEND_URL!; // 예: https://api-inha-borrow.duckdns.org

async function proxy(
    req: NextRequest,
    ctx: { params: Promise<{ path: string[] }> }, // ✅ params가 Promise
) {
    const { path } = await ctx.params; // ✅ 여기서 풀어줌
    const pathname = path.join("/");
    const url = `${BACKEND}/${pathname}${req.nextUrl.search}`;

    const method = req.method;
    const body = method === "GET" || method === "HEAD" ? undefined : await req.arrayBuffer();

    const upstream = await fetch(url, {
        method,
        headers: {
            // 필요한 헤더만 전달 (과하게 복사하면 문제 생길 때 많음)
            "content-type": req.headers.get("content-type") ?? "",
            cookie: req.headers.get("cookie") ?? "",
            authorization: req.headers.get("authorization") ?? "",
        },
        body,
        redirect: "manual",
    });

    const resBody = await upstream.arrayBuffer();
    const res = new NextResponse(resBody, { status: upstream.status });

    // content-type 전달
    const ct = upstream.headers.get("content-type");
    if (ct) res.headers.set("content-type", ct);

    // ✅ Set-Cookie 여러 개도 처리 (가능하면)
    const getSetCookie = (upstream.headers as any).getSetCookie?.bind(upstream.headers);
    if (getSetCookie) {
        const cookies: string[] = getSetCookie();
        for (const c of cookies) res.headers.append("set-cookie", c);
    } else {
        const sc = upstream.headers.get("set-cookie");
        if (sc) res.headers.set("set-cookie", sc);
    }

    return res;
}

export async function GET(req: NextRequest, ctx: any) {
    return proxy(req, ctx);
}
export async function POST(req: NextRequest, ctx: any) {
    return proxy(req, ctx);
}
export async function PUT(req: NextRequest, ctx: any) {
    return proxy(req, ctx);
}
export async function PATCH(req: NextRequest, ctx: any) {
    return proxy(req, ctx);
}
export async function DELETE(req: NextRequest, ctx: any) {
    return proxy(req, ctx);
}
