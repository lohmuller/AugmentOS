import { log } from "../utils/logger";

export const installApk = (): Promise<any> => {
  log.core.warn("installApk is not available on iOS");
  return Promise.reject("installApk is not available on iOS");
};

export const downloadCoreApk = () => {
  log.core.warn("downloadCoreApk is not available on iOS");
};

interface InstallApkModuleInterface {
  installApk: (packageName: string) => Promise<any>;
  downloadCoreApk: () => void;
}

const InstallApkModule = {
  installApk,
  downloadCoreApk,
} as InstallApkModuleInterface;

export default InstallApkModule;