import type { PluginOption } from "vite";
import compression from "vite-plugin-compression";

type ViteCompression = "gzip" | "brotli" | "both" | "none" | "gzip-clear" | "brotli-clear" | "both-clear";

export function configCompressPlugin(compress: ViteCompression): PluginOption | PluginOption[] {
  const compressList = compress.split(",");
  const plugins: PluginOption[] = [];

  if (compressList.includes("gzip")) {
    plugins.push(
      compression({
        ext: ".gz",
        deleteOriginFile: false
      })
    );
  }

  if (compressList.includes("brotli")) {
    plugins.push(
      compression({
        ext: ".br",
        algorithm: "brotliCompress",
        deleteOriginFile: false
      })
    );
  }

  if (compressList.includes("gzip-clear")) {
    plugins.push(
      compression({
        ext: ".gz",
        deleteOriginFile: true
      })
    );
  }

  if (compressList.includes("brotli-clear")) {
    plugins.push(
      compression({
        ext: ".br",
        algorithm: "brotliCompress",
        deleteOriginFile: true
      })
    );
  }

  return plugins;
}
