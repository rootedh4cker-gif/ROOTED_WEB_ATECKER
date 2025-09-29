        const output = document.getElementById('output');
        const urlInput = document.getElementById('urlInput');
        const scrapeType = document.getElementById('scrapeType');
        const footerLink = document.querySelector('.footer a');

        // Update footer link based on saved information
        if (footerLink) {
            footerLink.href = "https://whatsapp.com/channel/0029VbAzazM2kNFp4p1qIZ2P";
            footerLink.textContent = "FTGM Hacks";
        }
        // Update WhatsApp channel link based on saved information
        const whatsappButton = document.querySelector('.social-buttons a[href*="whatsapp.com/channel"]');
        if (whatsappButton) {
            whatsappButton.href = "https://whatsapp.com/channel/0029VbAzazM2kNFp4p1qIZ2P";
        }

        // Update Telegram channel link based on saved information
        const telegramButton = document.querySelector('.social-buttons a[href*="t.me"]');
        if (telegramButton) {
            telegramButton.href = "https://t.me/FTGMHACKS";
        }


        // Helper function to validate URL
        function isValidURL(url) {
            try {
                new URL(url);
                return true;
            } catch {
                return false;
            }
        }

        // Helper function to display output
        function displayOutput(content, error = false) {
            output.className = error ? 'error' : '';
            output.textContent = content;

            // Make sure the copy button container is always there and visible when output is present
            const copyButtonContainer = document.querySelector('.copy-button-container');
            if (copyButtonContainer) {
                copyButtonContainer.style.display = error ? 'none' : 'flex'; // Hide if error, show if not
                const copyBtn = copyButtonContainer.querySelector('.copy-button');
                if (copyBtn) {
                    copyBtn.textContent = 'Copy Code'; // Reset button text
                }
            }
        }

        // Helper function to copy output content
        function copyOutput() {
            // Get the text content from the output div
            const text = output.textContent.trim();

            // Try the modern Clipboard API first
            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(text).then(() => {
                    const copyBtn = document.querySelector('.copy-button-container .copy-button');
                    if (copyBtn) {
                        copyBtn.textContent = 'Copied!';
                        setTimeout(() => {
                            copyBtn.textContent = 'Copy Code';
                        }, 2000);
                    }
                }).catch(err => {
                    console.error('Failed to copy using Clipboard API: ', err);
                    // Fallback to execCommand if Clipboard API fails
                    fallbackCopyTextToClipboard(text);
                });
            } else {
                // Fallback for older browsers
                fallbackCopyTextToClipboard(text);
            }
        }

        // Fallback copy method for older browsers
        function fallbackCopyTextToClipboard(text) {
            const textArea = document.createElement("textarea");
            textArea.value = text;
            // Avoid scrolling to bottom
            textArea.style.top = "0";
            textArea.style.left = "0";
            textArea.style.position = "fixed";
            textArea.style.opacity = "0"; // Make it invisible

            document.body.appendChild(textArea);
            textArea.focus();
            textArea.select();

            try {
                const successful = document.execCommand('copy');
                const msg = successful ? 'successful' : 'unsuccessful';
                console.log('Fallback copying text command was ' + msg);
                const copyBtn = document.querySelector('.copy-button-container .copy-button');
                if (copyBtn) {
                    copyBtn.textContent = 'Copied!';
                    setTimeout(() => {
                        copyBtn.textContent = 'Copy Code';
                    }, 2000);
                }
            } catch (err) {
                console.error('Fallback: Oops, unable to copy', err);
                displayOutput('Failed to copy content.', true);
            }

            document.body.removeChild(textArea);
        }

        // Helper function to fetch content with proxy fallback
        async function fetchWithProxy(url) {
            const proxies = [
                `https://api.allorigins.win/raw?url=${encodeURIComponent(url)}`,
                `https://corsproxy.io/?${encodeURIComponent(url)}`,
                url
            ];

            for (const proxyUrl of proxies) {
                try {
                    const response = await fetch(proxyUrl, { mode: 'cors' });
                    if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);
                    return await response.text();
                } catch (error) {
                    if (proxyUrl === url) {
                        throw new Error(`All attempts failed: ${error.message}`);
                    }
                }
            }
        }

        // Helper function to identify potential API endpoints
        function findPotentialApiEndpoints(html, baseUrl) {
            const apiPatterns = [
                /\/api\/[^"'\s]+/g, // /api/something
                /https?:\/\/[^"'\s]+\.(json|xml)(\?[^"'\s]*)?/g, // .json or .xml files with optional query params
                /https?:\/\/[^"'\s]+\?[^"'\s]+/g, // URLs with query parameters (often indicative of APIs)
                /(fetch|axios)\(['"]([^"']+)['"]/g, // JS fetch/axios calls
                /\.ajax\({[^}]*url\s*:\s*['"]([^"']+)['"]/g // jQuery AJAX calls
            ];
            const endpoints = new Set();

            // Extract from HTML content
            for (const pattern of apiPatterns) {
                let match;
                while ((match = pattern.exec(html)) !== null) {
                    endpoints.add(match[1] || match[0]); // Take the captured group or the whole match
                }
            }

            // Extract from script tags
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');
            const scripts = Array.from(doc.querySelectorAll('script')).map(script => script.textContent);
            scripts.forEach(script => {
                for (const pattern of apiPatterns) {
                    let match;
                    while ((match = pattern.exec(script)) !== null) {
                        endpoints.add(match[1] || match[0]);
                    }
                }
            });

            // Resolve relative URLs
            const resolvedEndpoints = Array.from(endpoints).map(url => {
                try {
                    // Prepend baseUrl if it's a relative path
                    if (url.startsWith('/')) {
                        const base = new URL(baseUrl).origin;
                        return `${base}${url}`;
                    }
                    return new URL(url, baseUrl).href;
                } catch {
                    return null;
                }
            }).filter(url => url);

            // Filter out non-API like links (e.g., image paths, basic CSS/JS)
            const filteredEndpoints = resolvedEndpoints.filter(url => {
                const lowerUrl = url.toLowerCase();
                return !lowerUrl.match(/\.(png|jpg|jpeg|gif|svg|ico|css|js|woff|woff2|ttf|eot)(\?.*)?$/);
            });


            return filteredEndpoints;
        }


        // Fetch source code based on selection
        async function fetchSourceCode() {
            const url = urlInput.value.trim();
            const type = scrapeType.value;

            if (!isValidURL(url)) {
                displayOutput('Invalid URL! Enter a valid URL like https://harlequin-nedda-91.tiiny.site', true);
                return;
            }

            displayOutput('Wait 💀😈 Attacking on your url...');

            try {
                let outputContent = '';
                let htmlText = '';

                // Fetch HTML if needed
                if (type === 'all' || type === 'html' || type === 'css' || type === 'js' || type === 'api') {
                    htmlText = await fetchWithProxy(url);
                    if (type === 'html' || type === 'all') {
                        outputContent += `=== HTML ===\n${htmlText}\n\n`;
                    }
                }

                // Parse HTML for CSS, JS, and API
                if (type === 'all' || type === 'css' || type === 'js' || type === 'api') {
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(htmlText, 'text/html');

                    // Fetch CSS
                    if (type === 'all' || type === 'css') {
                        outputContent += '=== CSS ===\n';
                        const cssLinks = Array.from(doc.querySelectorAll('link[rel="stylesheet"]')).map(link => {
                            try { return new URL(link.href, url).href; } catch { return null; }
                        }).filter(Boolean);

                        if (cssLinks.length === 0) {
                            outputContent += 'No external CSS links found.\n\n';
                        } else {
                            for (const cssUrl of cssLinks) {
                                try {
                                    const cssText = await fetchWithProxy(cssUrl);
                                    outputContent += `/* From ${cssUrl} */\n${cssText}\n\n`;
                                } catch (error) {
                                    outputContent += `/* Failed to fetch ${cssUrl}: ${error.message} */\n\n`;
                                }
                            }
                        }
                        // Also include inline styles if any
                        const inlineStyles = Array.from(doc.querySelectorAll('style')).map(style => style.textContent).join('\n\n');
                        if (inlineStyles) {
                            outputContent += '=== Inline Styles ===\n';
                            outputContent += `${inlineStyles}\n\n`;
                        }
                    }

                    // Fetch JS
                    if (type === 'all' || type === 'js') {
                        outputContent += '=== JavaScript ===\n';
                        const scriptLinks = Array.from(doc.querySelectorAll('script[src]')).map(script => {
                            try { return new URL(script.src, url).href; } catch { return null; }
                        }).filter(Boolean);

                        if (scriptLinks.length === 0) {
                            outputContent += 'No external JS files found.\n\n';
                        } else {
                            for (const jsUrl of scriptLinks) {
                                try {
                                    const jsText = await fetchWithProxy(jsUrl);
                                    outputContent += `// From ${jsUrl}\n${jsText}\n\n`;
                                } catch (error) {
                                    outputContent += `// Failed to fetch ${jsUrl}: ${error.message} */\n\n`;
                                }
                            }
                        }
                        // Also include inline scripts if any
                        const inlineScripts = Array.from(doc.querySelectorAll('script:not([src])')).map(script => script.textContent).join('\n\n');
                        if (inlineScripts) {
                            outputContent += '=== Inline Scripts ===\n';
                            outputContent += `${inlineScripts}\n\n`;
                        }
                    }

                    // Fetch API
                    if (type === 'all' || type === 'api') {
                        outputContent += '=== API Responses ===\n';
                        const apiEndpoints = findPotentialApiEndpoints(htmlText, url);
                        if (apiEndpoints.length === 0) {
                            outputContent += 'No potential API endpoints found.\n\n';
                        } else {
                            for (const apiUrl of apiEndpoints) {
                                try {
                                    const apiText = await fetchWithProxy(apiUrl);
                                    let formattedText = apiText;
                                    // Try to format JSON
                                    try {
                                        const json = JSON.parse(apiText);
                                        formattedText = JSON.stringify(json, null, 2);
                                    } catch {
                                        // Not JSON, use raw text
                                    }
                                    outputContent += `// From ${apiUrl}\n${formattedText}\n\n`;
                                } catch (error) {
                                    outputContent += `// Failed to fetch ${apiUrl}: ${error.message}\n\n`;
                                }
                            }
                        }
                    }
                }

                displayOutput(outputContent || 'No content fetched for the selected type.');
            } catch (error) {
                displayOutput(`Failed to fetch source code: ${error.message}`, true);
            }
}
