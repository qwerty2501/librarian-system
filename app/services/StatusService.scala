package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import dao.{StatusDAO, UserDAO}
import forms.NewStatusForm
import models.{Status, StatusWithUser}
import utilities.ApplicationError

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@ImplementedBy(classOf[StatusServiceImpl])
trait StatusService {
  def getAllStatuses(userID:Int)(implicit executor:ExecutionContext):Future[Either[ApplicationError, Seq[StatusWithUser]]]
  def createStatus(userID:Int, newStatusForm: NewStatusForm)(implicit executor:ExecutionContext):Future[Either[ApplicationError,_]]
  def deleteStatus(userID:Int, statusID:Int)(implicit executor:ExecutionContext):Future[Either[ApplicationError,_]]
  def updateStatus(userID:Int,statusID:Int,newStatusForm:NewStatusForm)(implicit executor: ExecutionContext):Future[Either[ApplicationError,_]]
}


class StatusServiceImpl @Inject() (statusDAO: StatusDAO,userService: UserService,userDAO: UserDAO) extends StatusService{
  override def getAllStatuses(userID:Int)(implicit executor:ExecutionContext): Future[Either[ApplicationError,Seq[StatusWithUser]]] = {
    userService.existUser(userID).flatMap {
      case Left(error) => Future.apply(Left(error))
      case Right(exist) => {
        if(exist == false){
          Future.apply(Left(ApplicationError("ユーザが見つかりませんでした")))
        } else{
          statusDAO.getAll.flatMap{statuses=>
            userDAO.findIn(statuses.map{s=>s.userID}.distinct).map{users=>
              Right(statuses.map{status=>
                StatusWithUser(status.id,status.userID,users.filter(u=>u.id == status.userID).head.name,status.text)
              })
            }
          }
        }
      }
    }.recover{
      case e: Exception => Left(ApplicationError("データ処理に失敗しました",e))
    }
  }

  override def createStatus(userID:Int, newStatusForm: NewStatusForm)(implicit executor:ExecutionContext):Future[Either[ApplicationError,_]]={
    userService.existUser(userID).flatMap{
      case Left(error)=> Future.apply(Left(error))
      case Right(exist)=>{
        if(exist == false){
          Future.apply(Left(ApplicationError("ユーザが見つかりませんでした")))
        } else{
          statusDAO.insert(Status(0,userID,newStatusForm.text,null,null)).map{_=>
            Right(Nil)
          }
        }
      }
    }.recover{
      case e:Exception => Left(ApplicationError("データ処理に失敗しました",e))
    }
  }

  override def deleteStatus(userID: Int, statusID: Int)(implicit executor: ExecutionContext): Future[Either[ApplicationError, _]] = {
    userService.existUser(userID).flatMap {
      case Left(error)=> Future.apply(Left(error))
      case Right(exist)=>{
        if(exist == false){
          Future.apply(Left(ApplicationError("ユーザが見つかりませんでした")))
        } else{
          statusDAO.delete(statusID,userID).map(_ =>Right(Nil))
        }
      }
    }.recover{
      case e:Exception => Left(ApplicationError("データ処理に失敗しました",e))
    }
  }

  override def updateStatus(userID: Int, statusID: Int, newStatusForm: NewStatusForm)(implicit executor: ExecutionContext): Future[Either[ApplicationError, _]] = {
    userService.existUser(userID).flatMap{
      case Left(error)=>Future.apply(Left(error))
      case Right(exist)=>{
        if(exist == false){
          Future.apply(Left(ApplicationError("ユーザが見つかりませんでした")))
        } else{
          statusDAO.update(Status(statusID,userID,newStatusForm.text,null,null)).map(_=>Right(Nil))
        }
      }
    }.recover{
      case e:Exception => Left(ApplicationError("データ処理に失敗しました",e))
    }
  }
}