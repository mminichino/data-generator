'use client';

import { useEffect } from 'react';

// Client-only loader that ensures theme scripts (jQuery + plugins) load in order
// and only after the main wrapper exists. Prevents race conditions that could
// blank the page during auth route transitions.
export default function ThemeScripts() {
  useEffect(() => {
    if (typeof window === 'undefined') return;

    // Avoid double-initialization across fast refresh/navigations
    if ((window as any).__themeScriptsLoaded) return;

    let cancelled = false;

    const waitFor = (predicate: () => boolean, timeoutMs = 3000) =>
      new Promise<void>((resolve) => {
        const start = Date.now();
        const tick = () => {
          if (cancelled) return;
          if (predicate()) return resolve();
          if (Date.now() - start > timeoutMs) return resolve();
          setTimeout(tick, 25);
        };
        tick();
      });

    const loadScript = (src: string, id?: string) =>
      new Promise<void>((resolve) => {
        if (cancelled) return resolve();
        if (id && document.getElementById(id)) return resolve();
        const s = document.createElement('script');
        if (id) s.id = id;
        s.src = src;
        s.async = true;
        s.onload = () => resolve();
        s.onerror = () => resolve(); // don't block if a vendor script errors
        document.body.appendChild(s);
      });

    const run = async () => {
      // wait for app chrome container to exist
      await waitFor(() => !!document.getElementById('main-wrapper'));

      // Ensure DOM is mostly ready
      if (document.readyState !== 'complete') {
        await new Promise<void>((r) => window.addEventListener('load', () => r(), { once: true }));
      }

      // Load in strict order: jQuery -> global -> metisMenu -> bootstrap-select -> custom -> quixnav-init
      await loadScript('/theme/assets/js/lib/data-table/jquery-3.6.0.min.js', 'jquery-core');

      // Bridge $ to jQuery if needed
      (window as any).$ = (window as any).$ || (window as any).jQuery;

      await loadScript('/theme/vendor/global/global.min.js');
      await loadScript('/theme/vendor/metismenu/js/metisMenu.min.js');
      await loadScript('/theme/vendor/bootstrap-select/dist/js/bootstrap-select.min.js');

      // Before running theme initializers, ensure plugins exist to avoid runtime errors
      const $: any = (window as any).jQuery || (window as any).$;
      if ($ && $.fn && typeof $.fn.metisMenu === 'function') {
        // Initialize sidebar menu if present
        try {
          const mm = document.querySelector('.metismenu');
          if (mm && !((mm as any).__mmInit)) {
            ($('.metismenu') as any).metisMenu();
            (mm as any).__mmInit = true;
          }
        } catch (_) {
          // swallow plugin init errors; UI should still render
        }
      }

      await loadScript('/theme/js/custom.min.js');
      await loadScript('/theme/js/quixnav-init.js');

      (window as any).__themeScriptsLoaded = true;
    };

    run();

    return () => {
      cancelled = true;
    };
  }, []);

  return null;
}
