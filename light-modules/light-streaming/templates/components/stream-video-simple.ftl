[#-- Base Source: \mtk\templates\components\video.ftl --]
[#-- Modify [#assign video = damfn.getAsset(content.assetsource)] --}
[#-------------- INCLUDE AND ASSIGN PART --------------]
[#include "../../includes/init.ftl"]

[#-- CSS default --]
[#if !divClass?has_content]
  [#assign divClass = "video-container"]
[/#if]

[#-- video css classes --]
[#assign videoHtml = ""]
[#if content.type?exists]
  [#include "../../macros/video.ftl"]
  [#assign videoHtml][@video content/][/#assign]
[/#if]


[#-------------- RENDERING PART --------------]
[#-- Rendering: video item --]
<div ${divID!} class="${divClass}" ${content.scale?boolean?string("", 'style="display:inline-block"')}>
${videoHtml}
</div><!-- end ${divClass} -->
