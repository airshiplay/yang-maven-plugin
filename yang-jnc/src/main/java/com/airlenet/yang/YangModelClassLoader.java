package com.airlenet.yang;

import com.tailf.jnc.Capabilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class YangModelClassLoader extends URLClassLoader {

    private String yangPackage;
    private List<YangJarClassloader> yangJarClassloaders = new ArrayList<>();

    public YangModelClassLoader(String yangPackage, URL[] urls) {
        super(urls, null);
        this.yangPackage = yangPackage;
        try {
            Enumeration<URL> resources = new URLClassLoader(urls).findResources(yangPackage.replace(".", "/"));
            while (resources.hasMoreElements()) {
                String path = resources.nextElement().getPath();
                String url = path.substring(0, path.lastIndexOf("!"));
                yangJarClassloaders.add(new YangJarClassloader(yangPackage, new URL[]{new URL(url)}, this));
            }
        } catch (IOException e) {

        }
    }


    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            // 1. 如果类已经被加载过了，直接返回
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                try {
                    //
                    if (name.startsWith(yangPackage)) {
                        for (YangJarClassloader yangJarClassloader : yangJarClassloaders) {
                            try {
                                Class<?> loadClass = yangJarClassloader.loadClass(name);
                                if (loadClass != null) {
                                    return loadClass;
                                }
                            } catch (Exception e) {

                            }
                        }
                    } else {
                        return super.loadClass(name, resolve);
                    }
                    return super.loadClass(name, resolve);
                } catch (ClassNotFoundException e) {
                    return super.loadClass(name, resolve);
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }

    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    public Class<?> loadYangClass(List<Capabilities.Capa> capas, String name) throws ClassNotFoundException {

        YangJarClassloader yangJarClassloader = null;
        List<YangJarClassloader> yangJarClassloaders = this.yangJarClassloaders;
        int max = 0;
        for (YangJarClassloader jarClassloader : yangJarClassloaders) {
            if (jarClassloader.capaString == null) {
                int count = jarClassloader.getEQCount(capas);
                if (count > max) {
                    max = count;
                    yangJarClassloader = jarClassloader;
                }
            } else {
                if (capas.toString().equals(jarClassloader.capaString)) {
                    yangJarClassloader = jarClassloader;
                    break;
                }
            }

        }

        try {
            yangJarClassloader.capaString = capas.toString();
            Class<?> aClass = yangJarClassloader.loadClass(name);
            return aClass;
        } catch (ClassNotFoundException e) {
        }

        return super.loadClass(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    @Override
    public URL getResource(String name) {
        return super.getResource(name);
    }

    public static class YangJarClassloader extends URLClassLoader {
        private String yangPackage;
        public List<Capabilities.Capa> capaList = new ArrayList<>();
        public String capaString;

        public YangJarClassloader(String yangPackage, URL[] urls, ClassLoader parent) {
            super(urls, parent);
            this.yangPackage = yangPackage;
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getResourceAsStream("yang.module")))) {
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    capaList.add(Capabilities.Capa.valueOf(line));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            return super.loadClass(name, resolve);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                // 1. 如果类已经被加载过了，直接返回
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    if (name.startsWith(yangPackage)) {
                        return this.findClass(name);
                    } else {
                        return getParent().loadClass(name);
                    }
                }
                return c;
            }
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            return super.findClass(name);
        }

        @Override
        public URL getResource(String name) {
            return this.findResource(name);
        }

        public int getEQCount(List<Capabilities.Capa> capas) {
            int i = 0;
            for (Object capa : capas) {
                for (Capabilities.Capa mCapa : capaList) {
                    if (mCapa.toString().equals(capa.toString())) {
                        i++;
                    }
                }
            }
            return i;
        }
    }
}
