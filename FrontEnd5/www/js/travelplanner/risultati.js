/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

$(document).ready(function() {

    $(this).on("click", "a#open_table",function(){


        for(var i=0; i<tpresult.alt.length;i++) {
            var id = tpresult.alt[i].id;
            $('table#id_apertura_'+id).hide();
            //eval('percorsoNascondi' + id + '();');
        }

        $('table#'+ $(this).attr('class')).show();
        var s = $(this).attr('class').substring("id_apertura_".length);
        console.log(s);
        console.log('percorsoVisualizza' + s + '();');
        eval('percorsoVisualizza' + s + '();');
        tpresult.selected = s;



    });
    /*
    $(this).on("click", "a.occhio",function(){
       var opacity = $(this).css("opacity");

       if (opacity === '1'){
           $(this).css("opacity", "0.25");
		   eval('percorsoNascondi' + $(this).attr('percorso') + '();');
       }else{
           $(this).css("opacity", "1");
		   eval('percorsoVisualizza' + $(this).attr('percorso') + '();');
       }

    });
    */

    $(this).on("click", ".expand",function(){

        var id = $(this).attr('id');

        var stato_expand = $(this).parent().parent().find('tr.' + id).css("display");

       if(stato_expand === 'none'){
           $(this).parent().parent().find('tr.' + id).show();
           $(this).html($(this).html().replace('+', '-'));
       }else{
           $(this).parent().parent().find('tr.' + id).hide();
           $(this).html($(this).html().replace('-', '+'));
       }

    });


});