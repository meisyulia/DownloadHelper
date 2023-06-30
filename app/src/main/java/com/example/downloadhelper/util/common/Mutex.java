package com.example.downloadhelper.util.common;

/**
 * @Description:
 * @Author: gy
 * @CreateDate: 2022/4/8 17:20
 */
public class Mutex {
   private boolean syncLock;
   public Mutex() {
      // TODO Auto-generated constructor stub
      syncLock=false;
   }
   /**
    * @param timeout 等待锁的超时时间，单位：ms
    * @return true 成功
    *         false 失败
    **/
   public synchronized boolean lock(int timeout){
      if(syncLock==true){
         try{
            if(timeout==0){
               wait();
            }else{
               wait(timeout);
               if(syncLock==true){
                  return false;
               }
            }
         }catch(Exception e){
            e.printStackTrace();
            return false;
         }
      }
      syncLock=true;

      return syncLock;
   }

   public synchronized boolean isLocked(){
      return syncLock;
   }

   /**
    * unlock
    **/
   public synchronized void unlock(){
      syncLock=false;
      notify();
   }
}
