import { Component, HostListener, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Vms } from 'app/model/vms.model';
import {MatDialog, MAT_DIALOG_DATA} from '@angular/material/dialog';
import { Inject } from '@angular/core';
import { AlertService } from 'app/auth/authservices/alert.service';
import { TeamService } from '../services/team.service';
import { UsedResources } from 'app/model/UsedResources.model';
import { vmModelDTO } from 'app/model/vmModelDTO.model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-limit-dialog',
  templateUrl: './limit-dialog.component.html',
  styleUrls: ['./limit-dialog.component.css']
})
export class LimitDialogComponent implements OnInit {


  

  limitForm: FormGroup;

  vms: Vms[] = []; 
  alertACTIVEVMS : string = "";
  activevms : any = 0;
  alertRAM : string = "";
  RAMtotal : any = 0;
  alertnvcpu : string = "";
  nvcputotal : any = 0;
  alertTOTALVMS : string = "";
  TOTALVMS : any = 0;
  alertDISKSIZE : string = "";
  DISKSIZE : any = 0;
  nvcpu: any;
  RAM: any;
  Disksize: any;
  ActiveVms: any;
  TotalVms: any;
  vmstemp: Vms[];
  length : any;

  vmModel : vmModelDTO;
  used_resource: Observable <UsedResources>;
  used_resources : UsedResources;

  ram_consumption : any;
  nvcpu_consumption : any;
  disk_consumption : any;

  ram_left : any;
  nvcpu_left: any;
  disk_left: any;
  myvalue : any;
 a : any;
 b : any;
 c : any;
 d : any;
 e : any;
 g : any;

 count :any = 0;
 count2 : any = 0;
 count3 : any = 0;
  submitted = false;

  
  constructor(private alertService: AlertService, private teamService: TeamService, private formBuilder: FormBuilder, @Inject(MAT_DIALOG_DATA) public data: Vms, public dialog: MatDialog) {

  }

  ngOnInit() {

    this.limitForm = this.formBuilder.group({
      nvcpu: ['',Validators.required],
      RAM: ['',Validators.required],
      Disksize: ['',Validators.required],
      



      
    });
  
      
  let m = 0;

    Object.entries(this.data).forEach( s => {
    
    this.length = (s.length);
   if(m==0)
    this.used_resource = s[1];
    
    else
    this.vmModel = s[1];

    m++;
    })

    this.used_resource.subscribe(data => {this.used_resources = data;
    
    this.ram_left = (this.vmModel.maxRam - this.used_resources.ram);
    this.disk_left = (this.vmModel.maxDisk - this.used_resources.disk);
    this.nvcpu_left = ((this.vmModel.maxNVCpu - this.used_resources.nvcpu));


    this.ram_consumption = ((this.used_resources.ram)/this.vmModel.maxRam)*100;
    this.disk_consumption = ((this.used_resources.disk)/this.vmModel.maxDisk)*100;
    this.nvcpu_consumption = ((this.used_resources.nvcpu)/this.vmModel.maxNVCpu)*100;
    console.log(this.vmModel)

    console.log(this.used_resources);
    console.log(this.vmModel.maxNVCpu);
    console.log(this.ram_consumption);
    console.log(this.nvcpu_consumption);
    
  })
  }


  get f() { return this.limitForm.controls; }

close()
{
this.dialog.closeAll();
}


  valuechangeRam(newValue) {
  
   if(newValue!=undefined && this.count==0) {
     this.count = 1;
  this.a = newValue;

  this.ram_consumption = this.ram_consumption + ((newValue)/this.vmModel.maxRam)*100;

  this.b = ((newValue)/this.vmModel.maxRam)*100;}


  if(newValue!=this.a){
   this.ram_consumption = this.ram_consumption -this.b;
  
   this.ram_consumption = this.ram_consumption + ((newValue)/this.vmModel.maxRam)*100;

   this.b = ((newValue)/this.vmModel.maxRam)*100;
   this.a = newValue;
  }

  
}

valuechangeDisk(newValue) {
  
 

   if(newValue!=undefined && this.count2==0){
     this.count2 = 1;

     this.c = newValue;

 this.disk_consumption = this.disk_consumption + ((newValue)/this.vmModel.maxDisk)*100;
   
 this.d = ((newValue)/this.vmModel.maxDisk)*100;}


    if(newValue!=this.c){
   this.disk_consumption = this.disk_consumption - this.d;
  
   this.disk_consumption = this.disk_consumption + ((newValue)/this.vmModel.maxDisk)*100;

  this.d = ((newValue)/this.vmModel.maxDisk)*100;
  this.c = newValue;
  }

}


valuechangenvcpu(newValue) {
  
   if(newValue!=undefined && this.count3==0){
     this.count3 = 1;

     this.e = newValue;

  this.nvcpu_consumption = this.nvcpu_consumption + ((newValue)/this.vmModel.maxNVCpu)*100;

  this.g = ((newValue)/this.vmModel.maxNVCpu)*100;}
   

   
    if(newValue!=this.e){
   this.nvcpu_consumption = this.nvcpu_consumption - this.g;
  
   this.nvcpu_consumption = this.nvcpu_consumption + ((newValue)/this.vmModel.maxNVCpu)*100;

  this.e = ((newValue)/this.vmModel.maxNVCpu)*100;
  this.g = newValue;
  }

}


createvm() {
this.alertService.clear();
  
  this.submitted = true;

 if (this.limitForm.invalid) {
            return;
        }
    
        


  this.alertACTIVEVMS = "";
  this.alertDISKSIZE = "";
  this.alertRAM = "";
  this.alertTOTALVMS = "";
  this.alertnvcpu ="";

  this.activevms = 0;
  this.DISKSIZE = 0;
  this.RAMtotal = 0;
  this.TOTALVMS = 0;
  this.nvcputotal = 0;
 

if (this.nvcpu < this.nvcputotal)
{
  this.alertnvcpu = "Limite non consentito"
}

if (this.RAM < this.RAMtotal)
{
  this.alertRAM = "Limite non consentito"
}

if (this.ActiveVms < this.activevms)
{

this.alertACTIVEVMS = "Limite non consentito"

}

if (this.TotalVms< this.TOTALVMS)
{
  this.alertTOTALVMS = "Limite non consentito"

}


}
}