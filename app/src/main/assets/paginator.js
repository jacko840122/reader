function pageNavigation(a, c, i, j, h) {
    var f = null;
    try {
        window.bridge.showSource("99999999",document.getElementsByTagName('html')[0].innerHTML);
        if (system.usingContinuallyDiv) {
            var b = volume.chapters[a];
            b.element.style.display = "block"
        }
        f = $get("book_container");
        var g = Math.round(j);
        if (g != 0) {
            f.style.height = j + "px"
        } else {
            f.style.height = viewport.height + "px"
        }
        f.scrollTop = i;
        if (system.usingContinuallyDiv) {
            f.scrollLeft = "" + (a * system.screenWidth)
        }
    } catch (d) {
        window.bridge.e("Problem during pageNavigation():" + d)
    }
    scheduleTask(function () {
        if (h) {
            window.bridge.onTakeSnapshot(c)
        } else {
            window.bridge.onPageNavigationFinished(c)
        }
    }, Delay.SHORT)
};