<?php
// Demo data for PHP Open Redirect Companion — used with
// `./gradlew runIde` to capture the real Marketplace screenshot. Open
// this file, the warning should appear on the header() line inside
// redirect_after_login.

function redirect_after_login() {
    // Redirect target built directly from a superglobal -- FLAGGED.
    header('Location: ' . $_GET['returnUrl']);
    exit;
}

function redirect_after_login_safely() {
    $allowed = ['/dashboard', '/profile', '/settings'];
    $target = $_GET['returnUrl'] ?? '/dashboard';
    if (!in_array($target, $allowed, true)) {
        $target = '/dashboard';
    }
    // Validated against a strict allowlist first -- NOT flagged
    // (the header argument here is a local variable, not a
    // superglobal reference).
    header('Location: ' . $target);
    exit;
}
