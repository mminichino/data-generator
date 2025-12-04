import Script from 'next/script';

export default function ThemeScripts() {
    return (
        <>
            <Script id="jquery-core" src="/theme/assets/js/lib/data-table/jquery-3.6.0.min.js" strategy="afterInteractive"/>
            <Script id="jquery-global-bridge" strategy="afterInteractive">
                {`window.$ = window.$ || window.jQuery;`}
            </Script>
            <Script src="/theme/vendor/global/global.min.js" strategy="afterInteractive" />
            <Script src="/theme/vendor/metismenu/js/metisMenu.min.js" strategy="afterInteractive" />
            <Script src="/theme/vendor/bootstrap-select/dist/js/bootstrap-select.min.js" strategy="afterInteractive" />
            <Script src="/theme/js/custom.min.js" strategy="afterInteractive" />
            <Script src="/theme/js/quixnav-init.js" strategy="afterInteractive" />
        </>
    );
}
