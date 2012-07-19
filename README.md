Rain1Template
=============

a new way for a php template engine.
This is a plugin for eclipse. From Multipage editor example plugin.

First:
- I'm not a Java developer
- I've no idea what i'm to do.
- See http://www.rain1.it/rain1template/ (Italian language, but see the final example) for a example.

What to do I want?
- Open a html file
- The opened file in editor has now two tabs: source, template
- In source I want the real content of file: a pure html/php file
- In template I want a derived text from source with simple regexp

example:
Template:
Hello {$name}
{foreach $arr as $k=>$v}
{$k} => {$v}
{/foreach}
---------------
Source:
Hello <?php echo $name?>
<?php foreach($arr as $k=>$v):?>
<?php echo $k?> => <?php echo $v?>
<?php endforeach?>

known issues:
- Missed Syntax highlight for template tab (missed grammar)
- The file is always in "to save" status