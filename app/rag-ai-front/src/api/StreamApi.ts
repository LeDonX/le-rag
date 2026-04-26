import {fetchEventSource} from "@microsoft/fetch-event-source";
import {BASE_URL} from "@/http/config.ts";
import service from "@/http";

type ResultCallBack = (e: any | null) => void;

export interface StreamTask {
    request: Promise<void>;
    abort: () => void;
}

interface CreateStreamOptions {
    body: BodyInit;
    headers?: Record<string, string>;
    onMessage: ResultCallBack;
    onError: ResultCallBack;
    onClose: ResultCallBack;
}

const createStreamTask = (
    url: string,
    options: CreateStreamOptions
): StreamTask => {
    const ctrl = new AbortController();
    const request = fetchEventSource(url, {
        method: "POST",
        headers: {
            Accept: "text/event-stream",
            ...(options.headers ?? {}),
        },
        body: options.body,
        signal: ctrl.signal,
        openWhenHidden: true,
        async onopen(response: Response) {
            const contentType = response.headers.get("content-type") || "";
            if (response.status === 401) {
                const module = await import("@/api/authUtils");
                module.default();
                throw new Error("HTTP 401 Unauthorized");
            }
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            if (!contentType.startsWith("text/event-stream")) {
                throw new Error(`Expected text/event-stream, got ${contentType}`);
            }
        },
        onmessage(event) {
            options.onMessage(event);
        },
        onclose() {
            options.onClose(null);
        },
        onerror(err) {
            options.onError(err);
            throw err;
        },
    });
    return {
        request,
        abort: () => ctrl.abort(),
    };
};
export const postStreamChat = (
    author: string,
    onMessage: ResultCallBack,
    onError: ResultCallBack,
    onClose: ResultCallBack
): StreamTask => {
    return createStreamTask(`${BASE_URL}/post-chat`, {
        body: JSON.stringify({author}),
        headers: {
            "Content-Type": "application/json",
        },
        onMessage,
        onError,
        onClose,
    });
};
// 兼容两种调用方式：
// 1) getStreamChat(message, onMessage, onError, onClose)
// 2) getStreamChat(message, url, onMessage, onError, onClose, sources)
export function getStreamChat(
    message: string,
    onMessage: ResultCallBack,
    onError: ResultCallBack,
    onClose: ResultCallBack
): StreamTask;
export function getStreamChat(
    message: string,
    url: string,
    onMessage: ResultCallBack,
    onError: ResultCallBack,
    onClose: ResultCallBack,
    sources?: string[]
): StreamTask;
export function getStreamChat(
    message: string,
    arg2: string | ResultCallBack,
    arg3: ResultCallBack,
    arg4: ResultCallBack,
    arg5?: ResultCallBack,
    arg6?: string[]
): StreamTask {
    if (typeof arg2 === "string") {
        const url = arg2;
        const onMessage = arg3;
        const onError = arg4;
        if (!arg5) {
            throw new Error("onClose callback is required");
        }
        const onClose = arg5;
        const sources = arg6 ?? [];
        const formData = new FormData();
        formData.append("message", message);
        if (sources.length > 0) {
            sources.forEach((source) => {
                formData.append("sources", source);
            });
        }
        return createStreamTask(`${service.defaults.baseURL}${url}`, {
            body: formData,
            headers: {
                Authorization: `Bearer ${localStorage.getItem("token") || ""}`,
            },
            onMessage,
            onError,
            onClose,
        });
    }
    const onMessage = arg2;
    const onError = arg3;
    const onClose = arg4;
    const formData = new FormData();
    formData.append("message", message);
    return createStreamTask(`${service.defaults.baseURL}/chat/stream`, {
        body: formData,
        headers: {
            Authorization: `Bearer ${localStorage.getItem("token") || ""}`,
        },
        onMessage,
        onError,
        onClose,
    });
}

export const postStreamChatWithSources = (
    message: string,
    sources: string[],
    url: string = "/ai/rag",
    onMessage: ResultCallBack,
    onError: ResultCallBack,
    onClose: ResultCallBack
): StreamTask => {
    return getStreamChat(message, url, onMessage, onError, onClose, sources);
};