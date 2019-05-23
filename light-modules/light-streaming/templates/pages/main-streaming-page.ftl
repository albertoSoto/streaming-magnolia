<!DOCTYPE html>
<html xml:lang="${cmsfn.language()}" lang="${cmsfn.language()}">
<head>
[@cms.page /]
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <title>${content.windowTitle!content.title!}</title>
    <meta name="description" content="${content.description!""}"/>
    <meta name="keywords" content="${content.keywords!""}"/>

 [#--   <link href="https://vjs.zencdn.net/7.5.4/video-js.css" rel="stylesheet">
    <!-- If you'd like to support IE8 (for Video.js versions prior to v7) -->
    <script src="https://vjs.zencdn.net/ie8/1.1.2/videojs-ie8.min.js"></script>
    --]


[#-- To load resources you can link them manually (e.g. line below) --]
[#-- <link rel="stylesheet" type="text/css" href="${ctx.contextPath}/.resources/light-streaming/webresources/css/bootstrap.css" media="all" /> --]
[#-- <script src="${ctx.contextPath}/.resources/light-streaming/webresources/js/jquery.js"></script> --]
[#-- or via theme --]
[#-- [#assign site = sitefn.site()!] --]
[#-- [#assign theme = sitefn.theme(site)!] --]
[#-- [#list theme.cssFiles as cssFile] --]
[#--   [#if cssFile.conditionalComment?has_content]<!--[if ${cssFile.conditionalComment}]>[/#if] --]
[#--     <link rel="stylesheet" type="text/css" href="${cssFile.link}" media="${cssFile.media}" /> --]
[#--   [#if cssFile.conditionalComment?has_content]<![endif]-->[/#if] --]
[#-- [/#list] --]
[#-- [#list theme.jsFiles as jsFile] --]
[#--   <script src="${jsFile.link}"></script> --]
[#-- [/#list] --]
[#-- uncomment next line to use resfn templating functions to load all css which matches a globbing pattern --]
[#-- ${resfn.css(["/light-streaming/.*css"])!} --]
</head>
<body class="main-streaming-page ${cmsfn.language()}">

<div class="container">
    <h1>${ctx.contextPath}</h1>
</div>
[@cms.area name="main"/]
[#-- use resfn to load all js which matches the globbing pattern or link resources manually or via theme --]
[#-- ${resfn.js(["/light-streaming/.*js"])!} --]
[#--<script src='https://vjs.zencdn.net/7.5.4/video.js'></script>--]

</body>
</html>

