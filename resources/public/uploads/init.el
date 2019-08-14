(require 'package)

;; Set proxies
(setq url-proxy-services
   '(("no_proxy" . "^\\(localhost\\|10.*\\)")
     ("http" . "172.22.1.4:8080")
     ("https" . "172.22.1.4:9443")))

;; Add package archives
(add-to-list 'package-archives
             '("melpa-stable" . "http://stable.melpa.org/packages/") t)
(package-initialize)

;; update the package metadata is the local cache is missing
(unless package-archive-contents
  (package-refresh-contents))

;; Always load newest byte code
(setq load-prefer-newer t)

(defvar my-packages
  '(;; key bindings and code colorization for Clojure
    clojure-mode

    ;; integration with a Clojure REPL
    cider

    ;; makes handling lisp expressions much, much easier
    paredit
        
    ;; extra syntax highlighting for clojure
    company

    ;; colorful parenthesis matching
    rainbow-delimiters

    ;; Project navigatio
    projectile

    ;; magit
    magit))

;; Load all packages
(dolist (p my-packages)
  (when (not (package-installed-p p))
    (print (format "::-> Installing package: %s" p))
    (package-install p)))


;; Enter cider mode when entering the clojure major mode
(add-hook 'clojure-mode-hook #'cider-mode)

;; Enable paredit in Clojure buffers and the REPL
(add-hook 'cider-mode-hook #'paredit-mode)
(add-hook 'cider-repl-mode-hook #'paredit-mode)

;; Enable auto-completion with Company-Mode for all buffers
;; (add-hook 'after-init-hook 'global-company-mode)
(add-hook 'cider-mode-hook #'company-mode)
(add-hook 'cider-repl-mode-hook #'company-mode)


;; Enable rainbow delimiters
(add-hook 'cider-mode-hook #'rainbow-delimiters-mode)
(add-hook 'cider-repl-mode-hook #'rainbow-delimiters-mode)

;; Show parenthesis mode
(show-paren-mode 1)

;; Load neotree and configure icons
(require 'neotree)
(global-set-key [f8] 'neotree-toggle)
(setq neo-theme (if (display-graphic-p) 'icons 'arrow))



;; Custom
(custom-set-variables
 ;; custom-set-variables was added by Custom.
 ;; If you edit it by hand, you could mess it up, so be careful.
 ;; Your init file should contain only one such instance.
 ;; If there is more than one, they won't work right.
 '(custom-safe-themes
   (quote
    ("5f27195e3f4b85ac50c1e2fac080f0dd6535440891c54fcfa62cdcefedf56b1b" "0598c6a29e13e7112cfbc2f523e31927ab7dce56ebb2016b567e1eff6dc1fd4f" "8aebf25556399b58091e533e455dd50a6a9cba958cc4ebb0aab175863c25b9a4" default)))
 '(package-selected-packages
   (quote
    (neotree monokai-theme cider spinner solarized-theme sesman seq rainbow-delimiters queue projectile paredit magit company clojure-mode))))

(custom-set-faces
 ;; custom-set-faces was added by Custom.
 ;; If you edit it by hand, you could mess it up, so be careful.
 ;; Your init file should contain only one such instance.
 ;; If there is more than one, they won't work right.
 )
